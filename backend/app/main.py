from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from .config import settings
from .database import init_db
from .routes import users_router, wishlists_router, gifts_router, reservation_router

app = FastAPI(
	title= settings.app_name,
	debug= True,
	docs_url='/api/docs',
	redoc_url='/api/redoc'
)
app.add_middleware(
		CORSMiddleware,
		allow_origins= settings.cors_origins,
		allow_credentials=True,
		allow_methods=["*"],
		allow_headers=["*"]
)

@app.get('/')
def root():
	return {
		"message": 'Wellcome!!!!!!!!!!!!!!!',
		"docs": "api/docs",
	}