export default function Dashboard() {
  return (
    <div className="grid grid-cols-3 gap-6">
      <Card title="Total Appointments" value="124" />
      <Card title="Pending Requests" value="8" />
      <Card title="Upcoming Today" value="5" />
    </div>
  );
}

function Card({ title, value }) {
  return (
    <div className="bg-card p-6 rounded-2xl shadow-sm border border-gray-100">
      <h3 className="text-muted text-sm mb-2">{title}</h3>
      <p className="text-3xl font-bold">{value}</p>
    </div>
  );
}