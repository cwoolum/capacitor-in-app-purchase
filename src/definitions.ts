declare module "@capacitor/core" {
  interface PluginRegistry {
    InAppPurchase: InAppPurchasePlugin;
  }
}

export interface InAppPurchasePlugin {
  echo(options: { value: string }): Promise<{value: string}>;
}
