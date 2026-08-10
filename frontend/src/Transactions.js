import axios from "./api/axios"
import {useState, useEffect} from "react";
import useAuth from "./hooks/useAuth";
import TransactionTable from "./TransactionTable";
import useRefreshToken from "./hooks/useRefreshToken";

const TRANSACTION_URL = "/transaction/"

const Transactions = () => {

    const {auth} = useAuth();
    const [transactions, setTransactions] = useState([])
    const refresh = useRefreshToken();
    const config = {
        headers: {Authorization: `Bearer ${auth.accessToken}`}
    };
    useEffect(() => {
        axios.get(TRANSACTION_URL, config)
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
            <button onClick={() => refresh()}></button>
        </section>
    );
}

export default Transactions;