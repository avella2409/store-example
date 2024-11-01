import {
  createBrowserRouter,
  RouterProvider,
} from "react-router-dom";
import './App.css';
import { EditProduct } from "./EditProduct.tsx";
import { Dashboard } from "./Dashboard.tsx";
import { Home } from './Home.tsx';
import { useKeycloak } from '../provider/KeycloakProvider.tsx';

const router = createBrowserRouter([
  {
    path: "/",
    element: <Home />,
    errorElement: <div>Error</div>,
    children: [
      {
        path: "dashboard",
        element: <Dashboard />
      },
      {
        path: "edit/product/:productId",
        element: <EditProduct />
      }
    ]
  }
]);

function App() {
  const keycloak = useKeycloak();

  return (
    <>
      {keycloak.authenticated ? <RouterProvider router={router} /> : <div>NOT AUTH</div>}
    </>
  )
}

export default App
