# Budget Balancer
A full-stack web application for budget management, with a Spring Boot REST API backend and React.js frontent.

## Table of Contents

- [About](#about)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## About

budget-balance is an application for tracking your budgets, through your accounts, transactions and debts. The financial forecaster can use your information to calculate when you can expect to pay off any debts, achieve savings goals and more. Currently, can be used locally or on the website (coming soon!).

## Features

- Transaction tracker
- Debts and accounts tracker
- Financial forecaster

## Installation

Clone the repository:

```bash
git clone https://github.com/davidhorobin/budget-balance.git
cd budget-balance
```

**Backend (Spring Boot)**

```bash
cd backend
./mvnw install
```
 
**Frontend (React)**
 
```bash
cd frontend
npm install
```

## Usage

**Run the backend**
 
```bash
cd backend
./mvnw spring-boot:run
```
 
The API will be available at `http://localhost:8080`.
 
**Run the frontend**
 
```bash
cd frontend
npm start
```
 
The app will be available at `http://localhost:3000`.

## Contributing

1. Fork the repo
2. Create a branch (`git checkout -b feature/x`)
3. Commit changes (`git commit -m 'Add x'`)
4. Push (`git push origin feature/x`)
5. Open a Pull Request

## License
 
Distributed under the MIT License. See `LICENSE` for details.
