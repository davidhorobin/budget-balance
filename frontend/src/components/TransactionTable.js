const TransactionTable = ({transactions}) => {
    return (
        <table id="transaction-table">
            <thead>
            <tr>
                <th>Time</th>
                <th>Counterparty</th>
                <th>Value</th>
            </tr>
            </thead>
            <tbody>
            {transactions.map((t, i) => (
                <tr key={i}>
                    <td>{t.time}</td>
                    <td>{t.counterparty}</td>
                    <td>{t.type === "Purchase" ? "-" : "+"}{t.value}</td>
                </tr>
            ))}
            </tbody>
        </table>
    );
}

export default TransactionTable;