import Login from "./Login";
import {Route, Routes} from "react-router-dom";
import usePersistLogin from "../hooks/usePersistLogin";
import Layout from "./Layout";
import Transactions from "./Transactions";
import RequireAuth from "./RequireAuth";
import Home from "./Home";
import Register from "./Register";
import Accounts from "./Accounts";

function App() {
    const isLoading = usePersistLogin();
    if (isLoading) return <p>Loading...</p>;
    return (
        <Routes>
            <Route path="/" element={<Layout/>}>
                {/* Public routes */}
                <Route path="login" element={<Login/>}/>
                <Route path="" element={<Home/>}/>
                <Route path="register" element={<Register/>}/>

                {/* Protected routes */}
                <Route element={<RequireAuth/>}>
                    <Route path="accounts/info" element={<Accounts/>}/>
                    <Route path="accounts/transactions" element={<Transactions/>}/>
                </Route>
            </Route>
        </Routes>
    );
}

export default App;
