const TransactionTable = ({transactions}) => {
    return (
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Counterparty</th>
                <th>Value</th>
                <th>Time</th>
            </tr>
            </thead>
            <tbody>
            {transactions.map((t) => (
                <tr key={t.id}>
                    <td>{t.id}</td>
                    <td>{t.counterparty}</td>
                    <td>{t.value}</td>
                    <td>{t.time}</td>
                </tr>
            ))}
            </tbody>
        </table>
    );
}

export default TransactionTable;