import { useNavigate } from "react-router-dom";
import { useServices } from "../provider/ServicesProvider";
import BasicModal from "./BasicModal";
import { ProductBoard } from "./ProductBoard";

export function Dashboard() {
    const navigate = useNavigate();
    const services = useServices();

    const createProduct = () =>
        services.productGateway.create()
            .then(productId => navigate(`/edit/product/${productId}?isNew=true`));

    return (
        <>
            <div className="flex gap-6">
                <BasicModal actionName="Create Product" title="Create Product" desc="Are you sure you want to create a new product?"
                    action={createProduct}></BasicModal>
            </div>
            <div className="mt-4">
                <ProductBoard></ProductBoard>
            </div>
        </>
    )
}