import { initializeApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';
import { getAuth } from 'firebase/auth';
import { getStorage } from 'firebase/storage';
const firebaseConfig = {
  apiKey: "AIzaSyAZVwVIUSpRbi-abwVmeFoLVAvK6Uzdxms",
  authDomain: "novafit-9b5a9.firebaseapp.com",
  projectId: "novafit-9b5a9",
  storageBucket: "novafit-9b5a9.firebasestorage.app",
  messagingSenderId: "180294789573",
  appId: "1:180294789573:web:f1e475e3e16bf8abab43ac"
};


const app = initializeApp(firebaseConfig);

export const db = getFirestore(app);
export const auth = getAuth(app);
export const storage = getStorage(app); 