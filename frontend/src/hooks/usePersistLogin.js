import {useEffect, useRef, useState} from "react";
import useRefreshToken from "./useRefreshToken";
import useAuth from "./useAuth";

const usePersistLogin = () => {
    const [isLoading, setIsLoading] = useState(true);
    const refresh = useRefreshToken();
    const {auth} = useAuth();
    const effectRan = useRef(false);

    useEffect(() => {
        if (effectRan.current === true || process.env.NODE_ENV !== "development") {
            const verifyRefreshToken = async () => {
                try {
                    const token = await refresh();
                    console.log("success:", token);
                } catch (e) {
                    console.error(e);
                } finally {
                    setIsLoading(false);
                }
            };

            !auth?.accessToken ? verifyRefreshToken() : setIsLoading(false);
        }

        return () => {
            effectRan.current = true;
        };
    }, []);

    return isLoading;
};

export default usePersistLogin;