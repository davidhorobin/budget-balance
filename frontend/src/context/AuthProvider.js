import {createContext, useState} from "react";

const AuthContext = createContext({});

// Session restore on page load is handled by usePersistLogin.
export const AuthProvider = ({children}) => {
    const [auth, setAuth] = useState({});

    return (
        <AuthContext.Provider value={{auth, setAuth}}>
            {children}
        </AuthContext.Provider>
    )
}

export default AuthContext;
