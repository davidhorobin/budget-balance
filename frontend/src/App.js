import Login from "./Login";
import {Routes, Route} from "react-router-dom";
import Layout from "./Layout";
import Transactions from "./Transactions";
import RequireAuth from "./RequireAuth";
import Home from "./Home";

function App() {
    return (
        <Routes>
            <Route path="/" element={<Layout/>}>
                {/* Public routes */}
                <Route path="login" element={<Login/>}/>
                <Route path="home" element={<Home/>}/>

                {/* Protected routes */}
                <Route element={<RequireAuth/>}>
                    <Route path="transaction" element={<Transactions/>}/>
                </Route>
            </Route>
        </Routes>
    );
}

export default App;
