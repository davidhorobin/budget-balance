import {createContext, useEffect, useState} from "react";
import useRefreshToken from "../hooks/useRefreshToken";

const AuthContext = createContext({});

export const AuthProvider = ({children}) => {
    const [auth, setAuth] = useState({});
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const init = async () => {
            try {
                // eslint-disable-next-line react-hooks/rules-of-hooks
                const data = await useRefreshToken();
                setAuth(data);
            } catch {
                setAuth({});
            } finally {
                setLoading(false);
            }
        };
        init();
    }, []);

    return (
        <AuthContext.Provider value={{auth, setAuth, loading}}>
            {children}
        </AuthContext.Provider>
    )
}

export default AuthContext;