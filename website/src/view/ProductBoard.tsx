import { Button, Card, CircularProgress } from '@mui/material';
import Avatar from '@mui/material/Avatar';
import Divider from '@mui/material/Divider';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import ListItemText from '@mui/material/ListItemText';
import Typography from '@mui/material/Typography';
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useServices } from '../provider/ServicesProvider';
import { Product } from '../service/ProductGateway';

export function ProductBoard() {
    const [products, setProducts] = useState<Product[]>([]);
    const services = useServices();
    const navigate = useNavigate();

    const [ready, setReady] = useState(false);

    const updateProducts = () => {
        setReady(false);
        services.productGateway.findAll()
            .then(ps => setProducts(ps))
            .finally(() => setReady(true));
    }

    useEffect(updateProducts, [])

    const archiveProduct = (productId: string) =>
        services.productGateway.archive(productId)
            .then(() => updateProducts())

    const publishProduct = (productId: string) =>
        services.productGateway.publish(productId)
            .then(() => updateProducts())

    const statusAction = (product: Product) => {
        switch (product.status) {
            case "DRAFT":
                return (
                    <div className='flex gap-4'>
                        <Button color="info" variant="contained" onClick={() => navigate(`/edit/product/${product.id}`)}>Update</Button>
                        <Button color="success" variant="contained" onClick={() => publishProduct(product.id)}>Publish</Button>
                    </div>
                );
            case "PUBLISHED":
                return (<div className='flex gap-4'>
                    <Button color="error" variant="contained" onClick={() => archiveProduct(product.id)}>Archive</Button>
                    <Button color="secondary" variant="contained" onClick={() => navigate(`/edit/product/${product.id}`)}>Copy</Button>
                </div>);
            case "ARCHIVED":
                return (<Button color="secondary" variant="contained" onClick={() => navigate(`/edit/product/${product.id}`)}>Copy</Button>);
            default:
                throw new Error("Invalid product status")
        }
    };

    return (
        <Card className="p-6" variant="outlined">
            <h4 className="font-semibold text-xl text-blue-700">Products</h4>
            {ready ?
                <List sx={{ width: '100%', bgcolor: 'background.paper' }}>
                    {products.map(product => (
                        <div key={product.id} >
                            <ListItem alignItems="flex-start">
                                <ListItemAvatar>
                                    <Avatar alt="Remy Sharp" src="/static/images/avatar/1.jpg" />
                                </ListItemAvatar>
                                <ListItemText
                                    primary={product.status}
                                    secondary={
                                        <React.Fragment>
                                            <Typography
                                                component="span"
                                                variant="body2"
                                                sx={{ color: 'text.primary', display: 'inline' }}
                                            >
                                                {product.id}
                                            </Typography>
                                        </React.Fragment>
                                    }
                                />
                                {statusAction(product)}
                            </ListItem>
                            <Divider variant="inset" component="li" />
                        </div>
                    ))}
                </List>
                : <div className="mt-6 flex justify-center"><CircularProgress /></div>}
        </Card>
    )
}