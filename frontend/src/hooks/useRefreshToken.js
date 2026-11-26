import axios from "../api/axios"
import useAuth from "./useAuth"

// Shared across all callers so concurrent 401s trigger one refresh; a second refresh
// with the already-rotated cookie would be rejected.
let inFlight = null;

const useRefreshToken = () => {
    const {setAuth} = useAuth();

    return async () => {
        if (!inFlight) {
            inFlight = axios.post('/auth/refresh', {}, {withCredentials: true})
                .finally(() => { inFlight = null; });
        }
        const response = await inFlight;
        setAuth(prev => {
            return {...prev, accessToken: response.data.accessToken}
        });
        return response.data.accessToken;
    }
}

export default useRefreshToken;