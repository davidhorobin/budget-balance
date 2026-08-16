import Login from "./Login";
import {Routes, Route} from "react-router-dom";
import Layout from "./Layout";
import Transactions from "./Transactions";
import RequireAuth from "./RequireAuth";
import Home from "./Home";
import Register from "./Register";

function App() {
    return (
        <Routes>
            <Route path="/" element={<Layout/>}>
                {/* Public routes */}
                <Route path="login" element={<Login/>}/>
                <Route path="" element={<Home/>}/>
                <Route path="register" element={<Register/>}/>

                {/* Protected routes */}
                <Route element={<RequireAuth/>}>
                    <Route path="transaction" element={<Transactions/>}/>
                </Route>
            </Route>
        </Routes>
    );
}

export default App;
