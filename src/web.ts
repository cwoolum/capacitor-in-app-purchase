import { WebPlugin } from '@capacitor/core';
import { InAppPurchasePlugin, SkuType } from './definitions';

export class InAppPurchaseWeb extends WebPlugin implements InAppPurchasePlugin {
  constructor() {
    super({
      name: 'InAppPurchase',
      platforms: ['web']
    });
  }
  consumePurchase(purchaseToken: string): Promise<void> {
    throw new Error("Method not implemented.");
  }
  subscribe(type: SkuType, receipt: String): Promise<{ transactionId: String; productId: String; token: String; }> {
    throw new Error("Method not implemented.");
  }
  restorePurchases(): Promise<{ data: import("./definitions").PurchaseDetail[]; }> {
    throw new Error("Method not implemented.");
  }
  getSkuDetails(skus: string[], skuType: SkuType): Promise<{ data: import("./definitions").SkuDetail[]; }> {
    throw new Error("Method not implemented.");
  }
  initialize(): Promise<void> {
    throw new Error("Method not implemented.");
  }


}

const InAppPurchase = new InAppPurchaseWeb();

export { InAppPurchase };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(InAppPurchase);
