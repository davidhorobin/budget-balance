import Login from "./Login";
import {Routes, Route} from "react-router-dom";
import Layout from "./Layout";
import Transactions from "./Transactions";

function App() {
    return (
        <Routes>
            <Route path="/" element={<Layout/>}>
                {/* Public routes */}
                <Route path="login" element={<Login/>}/>
                <Route path="transaction" element={<Transactions/>}/>

                {/* Protected routes */}
                <Route/>
            </Route>
        </Routes>
    );
}

export default App;
