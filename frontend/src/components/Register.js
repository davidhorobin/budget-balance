import {useRef, useState, useEffect} from 'react'
import {Link} from "react-router-dom";
import axios from '../api/axios'

const USER_REGEX = /^[A-Za-z0-9]{2,24}$/
const PASS_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*]).{8,24}$/
const REGISTER_URL = '/auth/register'

const Register = () => {
    const userRef = useRef();
    const errRef = useRef();

    const [user, setUser] = useState('');
    const [validName, setValidName] = useState(false);
    const [userFocus, setUserFocus] = useState(false);

    const [pwd, setPwd] = useState('');
    const [validPwd, setValidPwd] = useState(false);
    const [pwdFocus, setPwdFocus] = useState(false);

    const [matchPwd, setMatchPwd] = useState('');
    const [validMatchPwd, setValidMatchPwd] = useState(false);
    const [matchFocus, setMatchFocus] = useState(false);

    const [errMsg, setErrMsg] = useState('');
    const [success, setSuccess] = useState(false);

    useEffect(() => {
        userRef.current.focus();
    }, [])

    useEffect(() => {
        const result = USER_REGEX.test(user);
        setValidName(result);
    }, [user])

    useEffect(() => {
        const result = PASS_REGEX.test(pwd);
        setValidPwd(result);
        const match = pwd === matchPwd;
        setValidMatchPwd(match);
    }, [pwd, matchPwd])

    useEffect(() => {
        setErrMsg('');
    }, [user, pwd, matchPwd])

    const handleSubmit = async (e) => {
        e.preventDefault();
        const v1 = USER_REGEX.test(user);
        const v2 = PASS_REGEX.test(pwd);
        if (!v1 || !v2) {
            setErrMsg('Invalid details');
            return;
        }
        try {
            await axios.post(REGISTER_URL,
                JSON.stringify({username: user, password: pwd, email: "brian@email.com"})
            );
            setSuccess(true);
        } catch (err) {
            if (!err?.response) {
                setErrMsg("No response from the server");
            } else if (err.response?.status === 409) {
                setErrMsg('Username already exists');
            }
        }
    }

    return (
        <>
            {success ? (
                <section>
                    <h1>Success!</h1>
                    <p>
                        <Link to="/login">Sign In</Link>
                    </p>
                </section>
            ) : (
                <section>
                    <p ref={errRef} className={errMsg ? "errmsg" : "offscreen"} aria-live="assertive">{errMsg}</p>
                    <h1>Register</h1>
                    <form onSubmit={handleSubmit}>
                        <label htmlFor="username">Username</label>
                        <input
                            type="text"
                            id="username"
                            ref={userRef}
                            autoComplete="off"
                            onChange={(e) => setUser(e.target.value)}
                            required
                            aria-invalid={validName ? "false" : "true"}
                            aria-describedby="uidnote"
                            onFocus={() => setUserFocus(true)}
                            onBlur={() => setUserFocus(false)}
                        />
                        <p id="uidnote" className={userFocus && user && !validName ? "instructions" : "offscreen"}>
                            Must be between 2 and 24 characters <br/>
                            Must only contain letters or numbers
                        </p>
                        <label htmlFor="password">Password</label>
                        <input
                            type="password"
                            id="password"
                            onChange={(e) => setPwd(e.target.value)}
                            required
                            aria-invalid={validPwd ? "false" : "true"}
                            aria-describedby="pwdnote"
                            onFocus={() => setPwdFocus(true)}
                            onBlur={() => setPwdFocus(false)}
                        />
                        <p id="pwdnote" className={pwdFocus && !validPwd ? "instructions" : "offscreen"}>
                            Must be between 8 and 24 characters <br/>
                            Must contain at least one uppercase letter, lowercase letter, number, and special character.
                        </p>
                        <label htmlFor="confirm_pwd">Confirm Password</label>
                        <input
                            type="password"
                            id="confirm_pwd"
                            onChange={(e) => setMatchPwd(e.target.value)}
                            required
                            aria-invalid={validMatchPwd ? "false" : "true"}
                            aria-describedby="confpwdnote"
                            onFocus={() => setMatchFocus(true)}
                            onBlur={() => setMatchFocus(false)}
                        />
                        <p id="confpwdnote" className={matchFocus && !validMatchPwd ? "instructions" : "offscreen"}>
                            Must match password
                        </p>
                        <button disabled={!validName || !validPwd || !validMatchPwd}>Register</button>
                    </form>
                    <p>Already registered?<br/>
                        <Link to="/login">Log In</Link>
                    </p>
                </section>
            )}
        </>
    )
}

export default Register;