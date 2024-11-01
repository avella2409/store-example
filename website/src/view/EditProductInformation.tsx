import { Button, Card, CircularProgress, TextField } from "@mui/material";
import { useEffect, useState } from "react";
import { useParams, useSearchParams } from "react-router-dom";
import { useServices } from "../provider/ServicesProvider";

export function EditProductInformation() {
    const services = useServices();
    const { productId } = useParams();

    if (productId === undefined) throw new Error("Product id should not be undefined");

    const [searchParams] = useSearchParams()

    const isNew: boolean = (searchParams.get("isNew") || "false") === "true";

    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const limitLengthName = 200;
    const limitLengthDescription = 1500;

    const [successMessage, setSuccessMessage] = useState<string | undefined>(undefined);

    const [ready, setReady] = useState(false);

    const updateProductInfo = () =>
        services.productInfoGateway.update(productId, {
            name,
            description
        }).then(() => {
            setSuccessMessage("Product info successfully updated")
            setTimeout(() => {
                setSuccessMessage(undefined)
            }, 2000);
        })

    useEffect(() => {
        if (!isNew)
            services.productInfoGateway.findInfo(productId)
                .then(info => {
                    setName(info.name);
                    setDescription(info.description);
                    setReady(true);
                })
        else setReady(true);
    }, [])

    return (
        <Card className="p-6" variant="outlined">
            <h4 className="font-semibold text-xl text-blue-700">Product Information</h4>
            {ready ? <div>
                <div className="mt-6 flex flex-col items-start gap-6">
                    <TextField
                        id="outlined-helperText"
                        label="Product Name"
                        placeholder="Name"
                        color="info"
                        helperText={`Customer facing name. Length: ${name.length}/${limitLengthName}`}
                        value={name}
                        onChange={e => setName(e.target.value.substring(0, limitLengthName))}
                    />

                    <TextField
                        className="w-full"
                        id="outlined-multiline-static-helperText"
                        label="Product Description"
                        multiline
                        color="info"
                        rows={4}
                        placeholder="Description"
                        helperText={`Customer facing description. Length: ${description.length}/${limitLengthDescription}`}
                        value={description}
                        onChange={e => setDescription(e.target.value.substring(0, limitLengthDescription))}
                    />
                </div>
                <div className="mt-4 flex gap-2 items-center">
                    <Button color="success" variant="contained" onClick={updateProductInfo}>Save</Button>
                    {successMessage !== undefined && <span className="text-green-500">{successMessage}</span>}
                </div>
            </div>
                : <div className="mt-6 flex justify-center"><CircularProgress /></div>}
        </Card>
    )
}