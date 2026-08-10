import axios from "./api/axios"
import {useState, useEffect} from "react";
import TransactionTable from "./TransactionTable";

const TRANSACTION_URL = "/transaction/"

const Transactions = () => {

    const [transactions, setTransactions] = useState([])

    useEffect(() => {
        axios.get(TRANSACTION_URL)
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
        </section>
    );
}

export default Transactions;