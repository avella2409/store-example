import { Product, ProductGateway } from "../ProductGateway";

export class FakeProductGateway implements ProductGateway {

    private readonly db: Product[] = [
        {
            id: "product1",
            status: "DRAFT"
        },
        {
            id: "product2",
            status: "PUBLISHED"
        },
        {
            id: "product3",
            status: "ARCHIVED"
        }
    ];


    publish(productId: string): Promise<void> {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                const product = this.db.find(p => p.id === productId);
                if (product) product.status = "PUBLISHED";
                resolve();
            }, 200);
        });
    }

    archive(productId: string): Promise<void> {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                const product = this.db.find(p => p.id === productId);
                if (product) product.status = "ARCHIVED";
                resolve();
            }, 200);
        });
    }

    create(): Promise<string> {
        return new Promise((resolve, _) => {
            const id = "product" + (this.db.length + 1);
            this.db.push({
                id,
                status: "DRAFT"
            });
            resolve(id);
        });
    }


    findAll(): Promise<Product[]> {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                resolve(this.db);
            }, 1000);
        });
    }

}