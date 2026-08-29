import {Link} from "react-router-dom";

const Header = () => {
    return (
        <section id="site-header">
            <h1>budget-balance</h1>
            <nav>
                <Link to="/">Home</Link>
                <Link to="/login">Sign in</Link>
                <Link to="/register">Register</Link>
            </nav>
        </section>
    );
}

export default Header;