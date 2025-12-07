import os
from typing import Optional
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    app_name: str = "Wishlist App"
    debug: bool = True
    database_url: str = "sqlite:///./wishlist.db"
    
    cors_origins: list = ["http://localhost:5173",
                          "http://localhost:3000",
                          "http://127.0.0.1:3000",
                           "http://127.0.0.1:5173"]
   
class Config:
    env_file = ".env"
    
settings = Settings()