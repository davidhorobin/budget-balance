const TransactionTable = ({transactions}) => {
    return (
        <table>
            <thead>
            <tr>
                <th>Time</th>
                <th>Counterparty</th>
                <th>Value</th>
            </tr>
            </thead>
            <tbody>
            {transactions.map((t) => (
                <tr>
                    <td>{t.time}</td>
                    <td>{t.counterparty}</td>
                    <td>{t.value}</td>
                </tr>
            ))}
            </tbody>
        </table>
    );
}

export default TransactionTable;