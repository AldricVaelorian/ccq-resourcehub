function App() {
  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-primary-600 text-white p-4">
        <div className="container mx-auto flex justify-between items-center">
          <h1 className="text-xl font-bold">ResourceHub</h1>
          <div className="flex space-x-4">
            <a href="#" className="hover:text-primary-200">Resources</a>
            <a href="#" className="hover:text-primary-200">Bookings</a>
            <a href="#" className="hover:text-primary-200">Profile</a>
          </div>
        </div>
      </nav>
      <main className="container mx-auto p-4">
        <h2 className="text-2xl font-bold mb-4">Welcome to ResourceHub</h2>
        <p className="text-gray-600">
          Your booking and lending system for shared resources.
        </p>
      </main>
    </div>
  )
}

export default App
