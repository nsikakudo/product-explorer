# Product Explorer


An Android application developed to showcase modern Android development best practices, featuring a clean MVVM architecture, offline-first data strategy, and comprehensive unit testing. The app explores an e-commerce catalog, fetching product data from the Fake Store API.

## Features

*   **Splash Screen**: A welcoming splash screen on app launch.
*   **Product Listing Screen**: View a comprehensive list of products.
*   **Product Detail View**: Tap on any product to view its full description, pricing, and rating information.
*   **Offline-First Cache**: Products are persisted locally using Room. The app is designed to:
    *   Display cached data immediately (if available) while attempting a network refresh.
    *   Show cached data when offline, along with a clear indication of the network state.
    *   Show an error and no data only if both network access fails and no cached data exists.
*   **Networking**: Uses Retrofit and OkHttp for safe and efficient API calls.

## Technical Requirements & Implementation

This project adheres to modern Android development best practices and the specified requirements:

*   **MVVM Architecture**: The application is structured using the Model-View-ViewModel architectural pattern, ensuring separation of concerns and testability.
*   **UI Implementation**: The user interface is built using XML Layouts and Android Fragments.
*   **SOLID Principles**:
    *   **Single Responsibility Principle (SRP)**: Classes and functions are designed to have a single, well-defined responsibility (e.g., `ProductApiService` for API calls, `ProductRepository` for data operations).
    *   **Open/Closed Principle (OCP)**: Achieved through interfaces and dependency injection, allowing extensions without modification of existing code.
    *   **Liskov Substitution Principle (LSP)**: `Resource` sealed class allows interchangeable handling of `Success`, `Error`, and `Loading` states.
    *   **Interface Segregation Principle (ISP)**: `ProductApiService` is focused solely on weather API interactions. `NetworkMonitor` is a small, specific interface.
    *   **Dependency Inversion Principle (DIP)**: High-level modules (`ViewModels`, `UseCases`) depend on abstractions (`ProductRepository`, `NetworkMonitor`) rather than concrete implementations.
*   **Dependency Injection (Hilt)**: Dagger Hilt is used for managing dependencies, making the codebase more modular, testable, and maintainable.
    *   `AppModule` provides all necessary dependencies like `OkHttpClient`, `Retrofit`, `Room Database`, `DAOs`, and `Repositories`.
    *   `BindingModule` maps interfaces to their concrete implementationsn which is crucial for decoupling and testability. it provides interfaces like `bindProductRepository` and `bindProductRemoteDataSource`.
*   **API Integration (fakestoreapi)**:
    *   Uses Retrofit for making HTTP requests to the fakestoreapi API.
    *   Error handling for API calls (network issues, HTTP errors) is implemented using a `safeApiCall` wrapper.
*   **Local Data Storage (Room Database)**:
    *   Room Persistence Library is used to store product details locally.
    *   `ProductEntity` defines the schema for product details.
    *   `ProductDao` provides methods for database interactions (insert and get).
*   **Asynchronous Operations (Kotlin Coroutines & Flow)**:
    *   All asynchronous operations (network calls, database interactions) are handled using Kotlin Coroutines.
    *   `Flow` is utilized for observing changes in product details and network connectivity.
*   **Navigation**: Navigation is managed by the Android Navigation Component with Fragments.   

## Setup and Installation

Follow these steps to run the application on your local machine.
*   **Prerequisites**:
    *   Android Studio IDE
    *   Android SDK (Target SDK 36, Min SDK 24)
 
*   **Steps to Run**:
    *   Clone the repo
    *   **Create local.properties**: The app uses a local.properties file in the project's root directory to manage the API base URL via BuildConfig. Create this file if it doesn't exist.
    *   **Add API BASE URL**: API_BASE_URL=https://fakestoreapi.com/
    *   sync and run.


## Testing

Unit tests are implemented using JUnit, Mockito-Kotlin, MockK, and Turbine to ensure the reliability of the core logic.
*   **Repository Testing (ProductRepositoryImplTest)**: Verifies the offline-first logic, ensuring correct behavior when online, offline with cache, and offline without cache.
*   **ViewModel Testing (ProductViewModelTest)**: Verifies that the ViewModel correctly maps repository results to UI-consumable states (Resource sealed class) and manages the StateFlows.


## Trade-offs and Future Improvements

*   **Trade-offs**:
    *   **Shared ViewModel Scope**: The ProductViewModel is scoped to the Activity using by activityViewModels(). This simplifies data sharing between the master-detail fragments. For a more robust solution, the ProductDetailsFragment should use Navigation's SavedStateHandle to receive the product ID argument and trigger its own detail fetch, scoping its ViewModel only to the fragment life cycle.
    *   **Redundant Use Case Layer**: The ProductCallUseCases class currently only delegates calls to the repository. While often included for architectural consistency, in this small project, it could have been omitted to simplify the flow from ViewModel to Repository.

*   **Future Improvements**:
    *   **Swipe-to-Refresh**: Implement a SwipeRefreshLayout in the list screen for manual data refresh.
    *   **UI Testing**: Add instrumentation tests (UI tests) using Espresso to cover fragment navigation, list item clicks, and UI state changes.


## Screenshots

The application supports both light and dark themes, adapting automatically to the user's system settings.

| Light Mode                                     | Dark Mode                                    |
| ---------------------------------------------- | -------------------------------------------- |
| ![wsplash](https://github.com/user-attachments/assets/c006226e-49cc-4d08-9fcc-688f8bd5ea57)      | ![bsplash](https://github.com/user-attachments/assets/22de3150-cae9-4575-9daa-abc2153739f4)      |
| ![wlist](https://github.com/user-attachments/assets/20b82f71-ff09-4ab5-afd3-9ad52bcaa7d1)      | ![blist](https://github.com/user-attachments/assets/026f154d-28c0-4528-95b8-a39738b36c03)      |
| ![wdetails](https://github.com/user-attachments/assets/d1c3a4a8-11a1-4e4a-9d40-b762052c1f71)      | ![bdetails](https://github.com/user-attachments/assets/48d64729-948c-40a9-bbdd-f021196524e0)      |

