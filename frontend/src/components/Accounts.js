import useAxiosPrivate from '../hooks/useAxiosPrivate';
import {useEffect, useState} from 'react'
import AccountTable from './AccountTable';

const INFO_URL = "/accounts/info"
const CREATE_URL = "/accounts/new"
const CURRENCIES = ["GBP", "USD", "EUR"]; // must match the bank_accounts currency CHECK

const Accounts = () => {
    const axiosPrivate = useAxiosPrivate();
    const [total, setTotal] = useState(0);
    const [accounts, setAccounts] = useState([]);

    const [name, setName] = useState("");
    const [bank, setBank] = useState("");
    const [balance, setBalance] = useState("");
    const [currency, setCurrency] = useState(CURRENCIES[0]);
    const [errMsg, setErrMsg] = useState("");
    const [submitting, setSubmitting] = useState(false);

    const loadAccounts = () =>
        axiosPrivate.get(INFO_URL)
            .then((response) => {
                setAccounts(response?.data?.accounts);
                setTotal(response?.data?.sum);
            })
            .catch((error) => {
                console.log(error.stack);
            });

    useEffect(() => {
        loadAccounts();
    }, [])

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrMsg("");
        setSubmitting(true);
        try {
            await axiosPrivate.post(CREATE_URL, {name, bank, balance, currency});
            setName("");
            setBank("");
            setBalance("");
            await loadAccounts();
        } catch (err) {
            setErrMsg(!err?.response
                ? "No server response."
                : err.response.data?.message || "Could not create account.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <section>
            <h1>Account dashboard</h1>
            <p>Total: {total}</p>
            <AccountTable accounts={accounts}/>

            <form className="entry-form" onSubmit={handleSubmit}>
                <h2>New account</h2>
                <p className={errMsg ? "errmsg" : "offscreen"} aria-live="assertive">{errMsg}</p>

                <label htmlFor="acc-name">Account name</label>
                <input
                    id="acc-name"
                    type="text"
                    maxLength={100}
                    placeholder="e.g. Current"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    required
                />

                <label htmlFor="acc-bank">Bank</label>
                <input
                    id="acc-bank"
                    type="text"
                    maxLength={100}
                    placeholder="e.g. HSBC"
                    value={bank}
                    onChange={(e) => setBank(e.target.value)}
                    required
                />

                <label htmlFor="acc-balance">Opening balance</label>
                <input
                    id="acc-balance"
                    type="number"
                    step="0.01"
                    value={balance}
                    onChange={(e) => setBalance(e.target.value)}
                    required
                />

                <label htmlFor="acc-currency">Currency</label>
                <select id="acc-currency" value={currency} onChange={(e) => setCurrency(e.target.value)}>
                    {CURRENCIES.map((c) => <option key={c} value={c}>{c}</option>)}
                </select>

                <button disabled={submitting}>{submitting ? "Saving…" : "Add account"}</button>
            </form>
        </section>
    );
}

export default Accounts
