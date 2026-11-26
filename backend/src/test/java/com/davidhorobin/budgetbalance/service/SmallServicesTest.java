package com.davidhorobin.budgetbalance.service;

import com.davidhorobin.budgetbalance.entity.Counterparty;
import com.davidhorobin.budgetbalance.entity.User;
import com.davidhorobin.budgetbalance.repository.CounterpartyRepo;
import com.davidhorobin.budgetbalance.repository.UserRepo;
import com.davidhorobin.budgetbalance.security.userdetails.CustomUserDetailsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** CounterpartyService, UserService and CustomUserDetailsService: each only needs a test or two. */
class SmallServicesTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void counterpartyIsNormalisedAndReused() {
        CounterpartyRepo repo = mock(CounterpartyRepo.class);
        Counterparty existing = new Counterparty(1, "tesco");
        when(repo.findByName("tesco")).thenReturn(Optional.of(existing));

        assertThat(new CounterpartyService(repo).resolveOrCreateCounterparty("  TESCO ")).isSameAs(existing);
        verify(repo, never()).save(any());
    }

    @Test
    void unknownCounterpartyIsCreatedNormalised() {
        CounterpartyRepo repo = mock(CounterpartyRepo.class);
        when(repo.findByName("netflix")).thenReturn(Optional.empty());
        when(repo.save(any(Counterparty.class))).thenAnswer(inv -> inv.getArgument(0));

        Counterparty created = new CounterpartyService(repo).resolveOrCreateCounterparty("Netflix ");

        assertThat(created.getName()).isEqualTo("netflix");
    }

    @Test
    void currentUserComesFromSecurityContext() {
        User alice = new User();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(alice, null, List.of()));

        assertThat(new UserService().getCurrentUser()).isSameAs(alice);
    }

    @Test
    void currentUserThrowsWhenNotAuthenticated() {
        assertThatThrownBy(() -> new UserService().getCurrentUser()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void currentUserThrowsWhenPrincipalIsNotAUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("just-a-name", null, List.of()));

        assertThatThrownBy(() -> new UserService().getCurrentUser()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void userDetailsServiceLoadsByUsername() {
        UserRepo repo = mock(UserRepo.class);
        User alice = new User();
        when(repo.findByUsername("alice")).thenReturn(Optional.of(alice));

        assertThat(new CustomUserDetailsService(repo).loadUserByUsername("alice")).isSameAs(alice);
    }

    @Test
    void userDetailsServiceThrowsForUnknownUser() {
        UserRepo repo = mock(UserRepo.class);
        when(repo.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new CustomUserDetailsService(repo).loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
