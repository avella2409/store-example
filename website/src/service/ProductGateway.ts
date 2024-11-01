export type Product = {
    id: string;
    status: string;
}

export interface ProductGateway {
    publish(productId: string): Promise<void>

    archive(productId: string): Promise<void>

    create(): Promise<string>

    findAll(): Promise<Product[]>
}