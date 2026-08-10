import {useRef, useState, useEffect, useContext} from 'react'
import AuthContext from './context/AuthProvider'
import axios from './api/axios'

const LOGIN_URL = '/auth/login'

const Login = () => {
    const {setAuth} = useContext(AuthContext);

    const [user, setUser] = useState('');
    const [pwd, setPwd] = useState('');
    const [errMsg, setErrMsg] = useState('');
    const [success, setSuccess] = useState(false);

    useEffect(() => {
        setErrMsg('');
    }, [user, pwd])

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const res = await axios.post(
                LOGIN_URL,
                JSON.stringify({username: user, password: pwd}),
                {
                    headers: {'Content-Type': 'application/json'},
                    withCredentials: true
                }
            );
            const accessToken = res?.data?.access_token;
            const refreshToken = res?.data?.refresh_token;
            console.log(accessToken);
            console.log(refreshToken);
            setAuth({user, pwd, accessToken, refreshToken});
            setUser('');
            setPwd('');
            setSuccess(true);
        } catch (err) {
            console.log(err);
            if (!err?.response) {
                setErrMsg("No server response");
            } else if (err.response?.status === 400) {
                setErrMsg('Missing username or password');
            } else if (err.response?.status === 401) {
                setErrMsg('Invalid username or password');
            } else {
                setErrMsg('Login failed.');
            }
        }
    }

    return (
        <>
            {success ? (
                <section>
                    <h1>You are logged in!</h1>
                    <br/>
                    <p>
                        <a href="#">Go to Home</a>
                    </p>
                </section>
            ) : (
                <section>
                    <p className={"errmsg"} aria-live={"assertive"}>{errMsg}</p>
                    <h1>Sign in</h1>
                    <form onSubmit={handleSubmit}>
                        <label htmlFor="username">Username</label>
                        <input
                            type="text"
                            id="username"
                            autoComplete="off"
                            onChange={e => setUser(e.target.value)}
                            value={user}
                            required
                        />
                        <label htmlFor="password">Password</label>
                        <input
                            type="password"
                            id="password"
                            onChange={e => setPwd(e.target.value)}
                            value={pwd}
                            required
                        />
                        <button>Sign in</button>
                    </form>
                    <p>
                        Don't have an account?<br/>
                        <span>
                    <a href="#">Register</a>
                </span>
                    </p>
                </section>
            )}
        </>
    );

}

export default Login