import {Link} from "react-router-dom";

const Home = () => {
    return (
        <section>
            <h1>Welcome home.</h1>
            <nav>
                <Link to="/login">Sign in</Link>
                <Link to="/register">Register</Link>
                <Link to="/transaction">Transactions</Link>
            </nav>
        </section>
    );
}

export default Home;