import { ProductInfo, ProductInfoGateway } from "../ProductInfoGateway";

export class FakeProductInfoGateway implements ProductInfoGateway {

    private readonly db: Map<string, ProductInfo> = new Map();

    findInfo(id: string): Promise<ProductInfo> {
        return new Promise((resolve, reject) => {
            const info = this.db.get(id);

            setTimeout(() => {
                if (info) resolve(info);
                else reject("Info no found");
            }, 1000);

        })
    }
    update(id: string, info: ProductInfo): Promise<void> {
        return new Promise((resolve, reject) => {
            this.db.set(id, info);
            resolve();
        })
    }

}