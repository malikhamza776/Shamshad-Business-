SHAMSHAD BUSINESS v3 — functional Firebase business-management foundation.

Modules:
- Email/password login and signup
- Per-user cloud data
- Items/stock
- Purchase: adds stock
- Sale: reduces stock
- Customers and suppliers
- Cash/remaining balances
- Dashboard totals
- Profit estimate
- Basic transaction history

Important:
This is a source project, not a prebuilt Play Store AAB. It must be built and tested in Android Studio before release.


## Added in v4
- Paisa Wasool / Cash Received section
- Records customer name, amount, optional detail, and server timestamp
- Saves receipt in the user's transactions collection with type `receipt`
- Reports include these receipts in Cash received


## v5 change
- Paisa Wasool now automatically updates the matching customer's `balance` by subtracting the received amount.
- The receipt is still recorded in transaction history.
- Customer matching uses the exact customer `name` stored in Customers.
