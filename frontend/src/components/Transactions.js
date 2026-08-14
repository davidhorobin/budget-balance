import {useState, useEffect} from "react";
import useAuth from "../hooks/useAuth";
import TransactionTable from "./TransactionTable";
import useAxiosPrivate from "../hooks/useAxiosPrivate";
import {Link} from "react-router-dom";

const TRANSACTION_URL = "/transaction/"

const Transactions = () => {

    const axiosPrivate = useAxiosPrivate();
    const {auth} = useAuth();
    const [transactions, setTransactions] = useState([])
    const config = {
        headers: {Authorization: `Bearer ${auth.accessToken}`}
    };

    useEffect(() => {
        axiosPrivate.get(TRANSACTION_URL, config)
            .then((res) => {
                setTransactions(res.data);
            })
            .catch((err) => {
                console.log(err.stack);
            })
    }, [])

    return (
        <section>
            <TransactionTable transactions={transactions}/>
            <Link to="/home">CLICK</Link>
        </section>
    );
}

export default Transactions;