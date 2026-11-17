# Android API contract

The Retrofit interfaces follow the FastAPI routes under `backend/app/routes`.
JSON field names are defined in `data/model/dto`; UI screens consume domain models.

## Testing modes

- Debug defaults to fake wishlist, gift, and reservation repositories. Server registration and login still use `API_BASE_URL`; if the server is unavailable, choose one of the explicit demo accounts on the login screen instead. Demo accounts use the bundled fixture owner IDs and do not create or send a JWT.
- Use Profile → Logout to clear either kind of session and switch demo personas. Fake data is persisted locally, so app restarts do not reset changes to the fixtures.
- To exercise the backend for both auth and data on an emulator, build debug with `-PwishlistNetworkDataSource=true -PwishlistDebugApiUrl=http://10.0.2.2:8000/` while the API runs on the host. Release uses the network source and has no demo entry.

## Routes

- Auth: `POST /users/register`, `POST /users/token`, `GET /users/me`.
- Wishlists: `GET /wishlists/{id}`, `GET /wishlists/link/{link}`, `POST /wishlists/`, `PATCH /wishlists/{id}`, `POST /wishlists/{id}/regenerate-link`, `DELETE /wishlists/{id}`.
- Gifts: `GET /gifts/wishlist/{id}`, `GET /gifts/{id}`, `POST /gifts/`, `PATCH /gifts/{id}`, `DELETE /gifts/{id}`.
- Reservations: `POST /reservations/gift/{id}`, `GET /reservations/my`, `POST /reservations/{id}/cancel`.

## Integration blockers

The backend's `GET /wishlists/my` declares a list response but currently returns an object containing a `wishlists` property. The client does not call this route. It reads the owner's wishlist IDs from `GET /users/me`, then fetches each `GET /wishlists/{id}` for full details. If the backend fixes the route, the client can switch to a direct list request.

`WishlistCreate` requires `user_id` even though the route obtains the owner from the bearer token. The client sends the session user ID to satisfy the current schema; the server should continue to derive ownership from the token.

The current `WishlistShort` response does not include gifts, so the client fetches full wishlist details for the owner's list. The debug variant defaults to the persistent fake source; set `wishlistNetworkDataSource=true` to exercise the network source. Release uses the network source.

The public-link cache holds only the last opened wishlist in memory and is scoped to the current session. After a restart, reservations on a friend's wishlist remain visible and cancellable, but the app can show only the gift ID until that public wishlist is opened again. `GET /gifts/{id}` is owner-only, so the client cannot recover a friend's gift details through that route. A reservation response containing a gift summary, or an authorized lookup for the reserver, is needed for complete details after restart.

The client clears session data on HTTP 401 and filters in-memory wishlist and reservation Flows by the active user ID. The token still resides in ordinary app preferences; encrypted or Keystore-backed storage remains to be implemented.
