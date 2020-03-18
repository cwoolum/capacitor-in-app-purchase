import { WebPlugin } from '@capacitor/core';
import { InAppPurchasePlugin } from './definitions';

export class InAppPurchaseWeb extends WebPlugin implements InAppPurchasePlugin {
  constructor() {
    super({
      name: 'InAppPurchase',
      platforms: ['web']
    });
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
