import {Link} from "react-router-dom";

const Home = () => {
    return (
        <section id="home">
            <h1>Welcome home.</h1>
            <Link to="/accounts/transactions">Transactions</Link>
            <Link to="/accounts/info">Dashboard</Link>
        </section>
    );
}

export default Home;