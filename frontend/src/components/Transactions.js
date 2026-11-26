import {useEffect, useState} from "react";
import {Link} from "react-router-dom";
import TransactionTable from "./TransactionTable";
import useAxiosPrivate from "../hooks/useAxiosPrivate";

const TRANSACTION_URL = "/transaction/"
const ACCOUNTS_URL = "/accounts/info"
const DEPOSIT_URL = "/accounts/deposit"

// Local time as "YYYY-MM-DDTHH:mm", the format datetime-local expects for max.
const nowLocal = () => {
    const d = new Date();
    d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
    return d.toISOString().slice(0, 16);
};

const Transactions = () => {

    const axiosPrivate = useAxiosPrivate();
    const [transactions, setTransactions] = useState([])
    const [accounts, setAccounts] = useState([]);

    const [type, setType] = useState("Purchase");
    const [accountName, setAccountName] = useState("");
    const [counterparty, setCounterparty] = useState("");
    const [amount, setAmount] = useState("");
    const [time, setTime] = useState("");
    const [errMsg, setErrMsg] = useState("");
    const [submitting, setSubmitting] = useState(false);

    const loadTransactions = () =>
        axiosPrivate.get(TRANSACTION_URL)
            .then((res) => setTransactions(res.data))
            .catch((err) => console.log(err.stack));

    useEffect(() => {
        loadTransactions();
        axiosPrivate.get(ACCOUNTS_URL)
            .then((res) => {
                const list = res?.data?.accounts ?? [];
                setAccounts(list);
                if (list.length) setAccountName(list[0].name);
            })
            .catch((err) => console.log(err.stack));
    }, [])

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrMsg("");
        setSubmitting(true);
        const account = accounts.find((a) => a.name === accountName);
        // Backend uses snake_case JSON.
        const common = {amount, currency: account.currency, time: time || null};
        try {
            if (type === "Purchase") {
                await axiosPrivate.post(TRANSACTION_URL, {...common, bank_account: accountName, counterparty});
            } else {
                await axiosPrivate.patch(DEPOSIT_URL, {...common, account_name: accountName});
            }
            setCounterparty("");
            setAmount("");
            setTime("");
            await loadTransactions();
        } catch (err) {
            setErrMsg(!err?.response
                ? "No server response."
                : err.response.data?.message || "Could not save transaction.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <section className="ledger-layout">
            {accounts.length === 0 ? (
                <p className="entry-form">
                    <span>Add a bank account on the <Link to="/accounts/info">Dashboard</Link> before recording transactions.</span>
                </p>
            ) : (
                <form className="entry-form" onSubmit={handleSubmit}>
                    <h2>New transaction</h2>
                    <p className={errMsg ? "errmsg" : "offscreen"} aria-live="assertive">{errMsg}</p>

                    <label htmlFor="tx-type">Type</label>
                    <select id="tx-type" value={type} onChange={(e) => setType(e.target.value)}>
                        <option value="Purchase">Purchase</option>
                        <option value="Deposit">Deposit</option>
                    </select>

                    <label htmlFor="tx-account">Account</label>
                    <select id="tx-account" value={accountName} onChange={(e) => setAccountName(e.target.value)}>
                        {accounts.map((a) => (
                            <option key={a.name} value={a.name}>{a.name} ({a.bank}, {a.currency})</option>
                        ))}
                    </select>

                    {type === "Purchase" && (
                        <>
                            <label htmlFor="tx-counterparty">Where did you spend it?</label>
                            <input
                                id="tx-counterparty"
                                type="text"
                                maxLength={100}
                                placeholder="e.g. Tesco, Netflix, landlord"
                                autoComplete="off"
                                value={counterparty}
                                onChange={(e) => setCounterparty(e.target.value)}
                                required
                            />
                        </>
                    )}

                    <label htmlFor="tx-amount">Amount</label>
                    <input
                        id="tx-amount"
                        type="number"
                        inputMode="decimal"
                        min="0.01"
                        step="0.01"
                        value={amount}
                        onChange={(e) => setAmount(e.target.value)}
                        required
                    />

                    <label htmlFor="tx-time">Date and time (optional, defaults to now)</label>
                    <input
                        id="tx-time"
                        type="datetime-local"
                        max={nowLocal()}
                        value={time}
                        onChange={(e) => setTime(e.target.value)}
                    />

                    <button disabled={submitting}>{submitting ? "Saving…" : "Add transaction"}</button>
                </form>
            )}
            <TransactionTable transactions={transactions}/>
        </section>
    );
}

export default Transactions;
