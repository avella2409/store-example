import { useParams } from "react-router-dom";
import { EditProductInformation } from "./EditProductInformation";

export function EditProduct() {
    const { productId } = useParams();

    return (
        <>
            <h4 className="font-bold text-xl">Edit Product: {productId}</h4>
            <div className="mt-6 flex flex-col gap-6">
                <EditProductInformation />
            </div>
        </>
    )
}