declare module "@capacitor/core" {
  interface PluginRegistry {
    InAppPurchase: InAppPurchasePlugin;
  }
}

export enum SkuType {
  InApp = "inapp",
  Subs = "subs"
}

export enum SubscriptionPeriod{
  P1W,
  P1M,
  P3M,
  P6M,
  P1Y
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
  subscriptionPeriod: SubscriptionPeriod
}

export interface InAppPurchasePlugin {
  consumePurchase(purchaseToken: string): Promise<void>;
  subscribe(type: SkuType, receipt: String): Promise<{ transactionId: String, productId: String, token: String }>;
  restorePurchases(): Promise<{ data: PurchaseDetail[] }>;
  getSkuDetails(skus: string[], skuType: SkuType): Promise<{ data: SkuDetail[] }>;
  initialize(): Promise<void>;
}
