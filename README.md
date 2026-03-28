# Fluxis - Modular Economy Ecosystem 🛡️📈

<p align="center">
  <img src="https://raw.githubusercontent.com/Levxys/Fluxis/main/icon.svg" width="128" height="128" alt="Fluxis Logo" />
</p>

Fluxis is a high-performance, modular economy plugin for Minecraft (Spigot/Paper) that brings **Dynamic Pricing**, **Stock-based Markets**, and **Advanced Trading** to your server. 

Designed for the **SkyWind Alliance**, Fluxis ensures a balanced, living economy where player actions directly influence market value.

---

## ✨ Features

### 🏦 Fluxis Core (The Heart)
- **Dynamic Price Engine**: Prices rise when players buy and fall when they sell.
- **Stock-based Vault**: Items aren't just deleted; they go into the "Fluxis Vault," creating a real supply and demand cycle.
- **Economic Drift**: Prices slowly return to their base value over time, ensuring long-term stability.
- **Server Activity Modifier**: Volatility scales with player count—making the market feel alive.

<p align="center">
  <img src="https://via.placeholder.com/800x400/1a237e/ffffff?text=Fluxis+Dynamic+Market+Preview" alt="Market Preview" />
  <br><i>Dynamic Market GUI with live pricing and trends</i>
</p>

### 📊 Fluxis+ (Visuals)
- **Live Graphs**: View last 10-minute price performance directly in the item lore with ASCII sparklines.
- **Trend Tracking**: Visual indicators (↑ Rising, ↓ Falling, → Stable) for every item.

<p align="center">
  <img src="https://via.placeholder.com/600x200/4fc3f7/000000?text=[+▂▃▅▇▆▅▃+]" alt="Sparkline Preview" />
  <br><i>Visual Price Performance Graphs (Fluxis+)</i>
</p>

### 🏛️ Fluxis Action (Auction House)
- **Global Bidding**: List items for auction and let the highest bidder win.
- **Paginated UI**: Supports thousands of listings with a high-performance GUI.
- **Persistent Storage**: All auctions are saved and survive server restarts.

<p align="center">
  <img src="https://via.placeholder.com/800x400/ffd700/000000?text=Fluxis+Auction+House+Preview" alt="Auction Preview" />
  <br><i>Modular Auction House System</i>
</p>

### 🤝 Fluxis TradeGUI (Safe Trading)
- **Secure P2P Trading**: A split-screen GUI for two players to exchange items and currency safely.
- **Ready-Check System**: Both players must confirm before the trade completes.
- **Anti-Scam Protection**: Any inventory closure cancels the trade and returns items to owners.

<p align="center">
  <img src="https://via.placeholder.com/800x400/c62828/ffffff?text=Fluxis+Safe+Trade+GUI" alt="Trade Preview" />
  <br><i>Secure 2-Player Trading Interface</i>
</p>

---

## 🛠️ Commands

| Command | Description |
| :--- | :--- |
| `/shop` | Open the Dynamic Market GUI |
| `/sell` | Open the Bulk Sell Vault |
| `/auction` | Open the Auction House |
| `/auction sell <price>` | List your hand item for auction |
| `/trade <player>` | Request a safe trade with another player |
| `/fluxis balance` | Check your current Money |

---

## 🔗 PlaceholderAPI Support
- `%fluxis_balance%` - Current player balance
- `%fluxis_market_price_<MATERIAL>%` - Current market price of an item
- `%fluxis_market_trend_<MATERIAL>%` - Visual trend of an item
- `%fluxis_market_stock_<MATERIAL>%` - Current stock level in the vault

---

## 🚀 Installation
1. Download the latest `.jar` from Modrinth or GitHub.
2. Place it in your `plugins/` folder.
3. (Optional) Install **PlaceholderAPI** for scoreboard integration.
4. Restart your server.

---

## 👨‍💻 Developer & Credits
- **Author**: Levxys (SkyWind Leader)
- **Project**: SkyWind Alliance Ecosystem
- **Values**: Justice, Equality, Integrity, Transparency.

---

## 📜 License
Fluxis is licensed under the **MIT License**. See `LICENSE` for details.
