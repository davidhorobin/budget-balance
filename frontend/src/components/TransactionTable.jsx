import TransactionRow from './TransactionRow';

function TransactionTable() {
    return (
        <table className="transaction-table">
            <tr>
                <th>Vendor</th>
                <th>Price</th>
                <th>Date</th>
            </tr>
            <TransactionRow value="10.72" vendor="Greggs" date="24-11-2006"/>
            <TransactionRow value="21.19" vendor="Tesco" date="25-11-2006"/>
            <TransactionRow value="34.49" vendor="Spoons" date="26-11-2006"/>
        </table>
    );
}

export default TransactionTable;