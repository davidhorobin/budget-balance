import axios from "../api/axios"
import useAuth from "./useAuth"

const useRefreshToken = () => {
    const {setAuth} = useAuth();
    const {auth} = useAuth()

    return async () => {
        const response = await axios.post('/auth/refresh', {}, {
            withCredentials: true
        });
        setAuth(prev => {
            return {...prev, accessToken: response.data.accessToken}
        });
        console.log(auth.accessToken);
        return response.data.accessToken;
    }
}

export default useRefreshToken;