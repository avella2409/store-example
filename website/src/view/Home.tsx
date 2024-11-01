import { Outlet } from "react-router-dom";
import NavBar from "./NavBar";

export function Home() {
    return (
        <>
            <NavBar name='Store'></NavBar>
            <div className="mt-6">
                <Outlet />
            </div>
        </>
    )
}