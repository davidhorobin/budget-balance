function TransactionRow({vendor, value, date}) {
    return (
        <tr>
            <td>{vendor}</td>
            <td>{value}</td>
            <td>{date}</td>
        </tr>
    );
}

export default TransactionRow;