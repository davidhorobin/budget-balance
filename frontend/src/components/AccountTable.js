const AccountTable = ({accounts}) => {
    return (
        <table id="account-table">
            <thead>
            <tr>
                <th>Bank</th>
                <th>Name</th>
                <th>Balance</th>
                <th>Currency</th>
            </tr>
            </thead>
            <tbody>
            {accounts.map((a) => (
                <tr>
                    <td>{a.bank}</td>
                    <td>{a.name}</td>
                    <td>{a.balance}</td>
                    <td>{a.currency}</td>
                </tr>
            ))}
            </tbody>
        </table>
    );
}

export default AccountTable;