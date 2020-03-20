import { WebPlugin } from '@capacitor/core';
import { InAppPurchasePlugin, SkuType } from './definitions';

export class InAppPurchaseWeb extends WebPlugin implements InAppPurchasePlugin {
  constructor() {
    super({
      name: 'InAppPurchase',
      platforms: ['web']
    });
  }
  consumePurchase(options: { purchaseToken: string; }): Promise<void> {
    throw new Error("Method not implemented.");
  }
  subscribe(options: { type: SkuType; receipt: String; }): Promise<{ transactionId: String; productId: String; token: String; }> {
    throw new Error("Method not implemented.");
  }
  restorePurchases(): Promise<{ data: import("./definitions").PurchaseDetail[]; }> {
    throw new Error("Method not implemented.");
  }
  getSkuDetails(options: { skus: string[]; skuType: SkuType; }): Promise<{ data: import("./definitions").SkuDetail[]; }> {
    throw new Error("Method not implemented.");
  }
  initialize(): Promise<void> {
    throw new Error("Method not implemented.");
  }

  async echo(options: { value: string }): Promise<{value: string}> {
    console.log('ECHO', options);
    return options;
  }
}

const InAppPurchase = new InAppPurchaseWeb();

export { InAppPurchase };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(InAppPurchase);
