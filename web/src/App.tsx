import { useState } from 'react'

function App() {
  const [count, setCount] = useState(0)

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      <div className="bg-white p-8 rounded-lg shadow-lg text-center">
        <h1 className="text-4xl font-bold text-primary-600 mb-4">
          Liyaqa Gym Management System
        </h1>
        <p className="text-gray-600 mb-6">
          Welcome to your gym management solution
        </p>
        <button
          onClick={() => setCount((count) => count + 1)}
          className="bg-primary-500 hover:bg-primary-600 text-white font-semibold py-2 px-6 rounded-lg transition-colors"
        >
          Count: {count}
        </button>
      </div>
    </div>
  )
}

export default App
