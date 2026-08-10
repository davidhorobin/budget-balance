import {Link} from "react-router-dom";

const Home = () => {
    return (
        <section>
            <h1>Welcome home.</h1>
            <Link to="/transaction">Transactions</Link>
        </section>
    );
}

export default Home;