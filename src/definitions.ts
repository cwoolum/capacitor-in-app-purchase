declare module "@capacitor/core" {
  interface PluginRegistry {
    InAppPurchase: InAppPurchasePlugin;
  }
}

export enum SkuType {
  InApp = "inapp",
  Subs = "subs"
}

export interface PurchaseDetail {
  orderId: string;
  packageName: string;
  productId: string;
  purchaseTime: Date;
  purchaseState: String;
  purchaseToken: string;
  signature: string;
  type: SkuType;
  receipt: string;
}

export interface SkuDetail {
  productId: String;
  title: String;
  description: String;
  price: number;
  type: SkuType,
  currency: string;
}

export interface InAppPurchasePlugin {
  consumePurchase(options: { purchaseToken: string }): Promise<void>;
  subscribe(options: { type: SkuType, receipt: String }): Promise<{ transactionId: String, productId: String, token: String }>;
  restorePurchases(): Promise<{ data: PurchaseDetail[] }>;
  getSkuDetails(options: { skus: string[], skuType: SkuType }): Promise<{ data: SkuDetail[] }>;
  initialize(): Promise<void>;
}
