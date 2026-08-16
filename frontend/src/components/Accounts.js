import useAxiosPrivate from '../hooks/useAxiosPrivate';
import useAuth from '../hooks/useAuth';
import {useState, useEffect} from 'react'
import AccountTable from './AccountTable';

const INFO_URL = "/accounts/info"

const Accounts = () => {
    const axiosPrivate = useAxiosPrivate();
    const {auth} = useAuth();
    const [total, setTotal] = useState(0);
    const [accounts, setAccounts] = useState([]);
    const config = {
        headers: {Authorization: `Bearer ${auth.accessToken}`}
    };

    useEffect(() => {
        axiosPrivate.get(INFO_URL, config)
            .then((response) => {
                setAccounts(response?.data?.accounts);
                setTotal(response?.data?.sum);
            })
            .catch((error) => {
                console.log(error.stack);
            })
    }, [])

    return (
        <section>
            <h1>Account dashboard</h1>
            <p>Total: {total}</p>
            <AccountTable accounts={accounts}/>
        </section>
    );
}

export default Accounts