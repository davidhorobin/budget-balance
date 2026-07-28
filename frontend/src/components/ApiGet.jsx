import { useState, useEffect } from 'react';
import axios from 'axios';
function ApiGet() {
    const [transactions, setTransactions] = useState([]);
    const [error, setError] = useState(null);

    useEffect(() => {
        axios.get("http://localhost:8080/transaction/")
            .then((response) => setTransactions(response.data))
            .catch((error) => {
                console.log(error);
                setError(error.message);
            });
    }, [])

    if (error) return <p>An error has occurred</p>;

    return (
        <table className="transaction-table">
            <thead>
                <tr>
                    <th>Vendor</th>
                    <th>Price</th>
                    <th>Time</th>
                </tr>
            </thead>
            <tbody>
            {transactions.map((t) => (
                <tr key = {t.id}>
                    <td>{t.vendor}</td>
                    <td>{t.value}</td>
                    <td>{t.time}</td>
                </tr>
            ))}
            </tbody>
        </table>
    );
}

export default ApiGet;