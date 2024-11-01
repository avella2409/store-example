export type ProductInfo = {
    name: string,
    description: string
}

export interface ProductInfoGateway {
    findInfo(id: string): Promise<ProductInfo>;

    update(id: string, info: ProductInfo): Promise<void>;
}